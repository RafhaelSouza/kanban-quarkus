package org.seariver.kanbanboard.write.domain.application;

import helper.TestHelper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.seariver.kanbanboard.write.domain.core.Bucket;
import org.seariver.kanbanboard.write.domain.core.WriteBucketRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Tag("unit")
public class CreateBucketCommandHandlerTest extends TestHelper {

    private ArgumentCaptor<Bucket> captor = ArgumentCaptor.forClass(Bucket.class);

    @Test
    void GIVEN_ValidCommand_MUST_CreateBucketInDatabase() {

        // given
        var uuid = UUID.randomUUID();
        var position = faker.number().randomDouble(3, 1, 10);
        var name = faker.pokemon().name();
        var command = new CreateBucketCommand(uuid, position, name);
        var repository = mock(WriteBucketRepository.class);

        // when
        var handler = new CreateBucketCommandHandler(repository);
        handler.handle(command);

        // then
        verify(repository).create(captor.capture());
        var bucket = captor.getValue();
        assertThat(bucket.getUuid()).isEqualTo(uuid);
        assertThat(bucket.getPosition()).isEqualTo(position);
        assertThat(bucket.getName()).isEqualTo(name);
    }
}
