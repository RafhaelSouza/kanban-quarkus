package org.seariver.kanbanboard.write.adapter.out;

import org.seariver.kanbanboard.write.domain.core.Bucket;
import org.seariver.kanbanboard.write.domain.core.WriteBucketRepository;
import org.seariver.kanbanboard.write.domain.exception.DuplicatedDataException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Singleton;
import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.seariver.kanbanboard.write.domain.exception.DomainException.Error.INVALID_DUPLICATED_DATA;

@Singleton
public class WriteBucketRepositoryImpl implements WriteBucketRepository {

    public static final String POSITION_FIELD = "position";
    public static final String UUID_FIELD = "uuid";
    public static final String NAME_FIELD = "name";

    private NamedParameterJdbcTemplate jdbcTemplate;

    public WriteBucketRepositoryImpl(DataSource dataSource) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void create(Bucket bucket) {

        try {
            var sql = "INSERT INTO bucket(uuid, position, name) values (:uuid, :position, :name)";

            MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue(UUID_FIELD, bucket.getUuid())
                .addValue(POSITION_FIELD, bucket.getPosition())
                .addValue(NAME_FIELD, bucket.getName());

            jdbcTemplate.update(sql, parameters);

        } catch (DuplicateKeyException exception) {

            var duplicatedDataException = new DuplicatedDataException(INVALID_DUPLICATED_DATA, exception);

            var existentBuckets = findByUuidOrPosition(bucket.getUuid(), bucket.getPosition());

            existentBuckets.forEach(existentBucket -> {

                if (existentBucket.getUuid().equals(bucket.getUuid())) {
                    duplicatedDataException.addError("id", bucket.getUuid());
                }

                if (existentBucket.getPosition() == bucket.getPosition()) {
                    duplicatedDataException.addError(POSITION_FIELD, bucket.getPosition());
                }
            });

            throw duplicatedDataException;
        }
    }

    @Override
    public void update(Bucket bucket) {

        var sql = "UPDATE bucket SET position = :position, name =:name WHERE uuid = :uuid";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue(UUID_FIELD, bucket.getUuid())
            .addValue(POSITION_FIELD, bucket.getPosition())
            .addValue(NAME_FIELD, bucket.getName());

        jdbcTemplate.update(sql, parameters);
    }

    public Optional<Bucket> findByUuid(UUID uuid) {

        var sql = "SELECT id, uuid, position, name, created_at, updated_at FROM bucket WHERE uuid = :uuid";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue(UUID_FIELD, uuid);

        return jdbcTemplate.query(sql, parameters, resultSet -> {

            if (resultSet.next()) {
                return Optional.of(new Bucket()
                    .setId(resultSet.getLong("id"))
                    .setUuid(UUID.fromString(resultSet.getString(UUID_FIELD)))
                    .setPosition(resultSet.getDouble(POSITION_FIELD))
                    .setName(resultSet.getString(NAME_FIELD))
                    .setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                    .setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime())
                );
            }

            return Optional.empty();
        });
    }

    public List<Bucket> findByUuidOrPosition(UUID uuid, double position) {

        var sql = "SELECT id, uuid, position, name, created_at, updated_at FROM bucket WHERE uuid = :uuid OR position = :position";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue(UUID_FIELD, uuid)
            .addValue(POSITION_FIELD, position);

        return jdbcTemplate.query(sql, parameters, (rs, rowNum) ->
            new Bucket()
                .setId(rs.getLong("id"))
                .setUuid(UUID.fromString(rs.getString(UUID_FIELD)))
                .setPosition(rs.getDouble(POSITION_FIELD))
                .setName(rs.getString(NAME_FIELD))
                .setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime())
                .setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
        );
    }
}
