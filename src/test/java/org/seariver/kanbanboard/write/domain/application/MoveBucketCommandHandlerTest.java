package org.seariver.kanbanboard.write.domain.application;

import helper.TestHelper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.seariver.kanbanboard.write.domain.core.Bucket;
import org.seariver.kanbanboard.write.domain.core.WriteBucketRepository;
import org.seariver.kanbanboard.write.domain.exception.BucketNotExistentException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
public class MoveBucketCommandHandlerTest extends TestHelper {

    @Test
    void GIVEN_ValidPosition_MUST_UpdateBucketPosition() {

        // given
        var uuid = UUID.randomUUID();
        var position = faker.number().randomDouble(3, 1, 10);
        var command = new MoveBucketCommand(uuid, position);
        var repository = mock(WriteBucketRepository.class);
        var bucket = new Bucket().setUuid(uuid).setPosition(123);
        when(repository.findByUuid(uuid)).thenReturn(Optional.of(bucket));

        // when
        var handler = new MoveBucketCommandHandler(repository);
        handler.handle(command);

        // then
        verify(repository).findByUuid(uuid);
        verify(repository).update(bucket);
        assertThat(bucket.getUuid()).isEqualTo(uuid);
        assertThat(bucket.getPosition()).isEqualTo(position);
    }

    @Test
    void GIVEN_NotExistentUuid_MUST_ThrowException() {

        // given
        var uuid = UUID.randomUUID();
        var position = faker.number().randomDouble(3, 1, 10);
        var command = new MoveBucketCommand(uuid, position);
        var repository = mock(WriteBucketRepository.class);
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        // when
        var handler = new MoveBucketCommandHandler(repository);
        var exception = assertThrows(
            BucketNotExistentException.class, () -> handler.handle(command));

        // then
        verify(repository).findByUuid(uuid);
        assertThat(exception.getMessage()).isEqualTo("Bucket not exist");
    }
}
