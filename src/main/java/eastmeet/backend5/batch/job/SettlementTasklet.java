package eastmeet.backend5.batch.job;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettlementTasklet implements Tasklet {

    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String rawDate = Objects.toString(chunkContext.getStepContext().getJobParameters().get("settlementDate"),
            LocalDate.now().toString()
        );

        LocalDate settlementDate;
        try {
            settlementDate = LocalDate.parse(rawDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("settlementDate must be yyyy-MM-dd: " + rawDate, e);
        }

        // TODO: 실제 정산 집계/원장 반영 로직으로 교체
        log.info("Settlement batch step started. settlementDate={}", settlementDate);
        log.info("Settlement batch step finished. settlementDate={}", settlementDate);

        return RepeatStatus.FINISHED;
    }
}
