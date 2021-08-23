import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class Event extends Task{
    protected LocalDateTime deadline;
    DateTimeFormatter fmt = new DateTimeFormatterBuilder()
            .appendPattern("MMM dd yyyy")
            .optionalStart()
            .appendPattern(" HHmm")
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter();

    Event(String desc, LocalDateTime deadline) {
        super(desc);
        this.deadline = deadline;
    }

    public LocalDateTime getDeadline() {
        return this.deadline;
    }

    @Override
    public String toString(){
        return "[E]" + "[" + this.getStatusIcon() + "]" + " " + this.description + "(" + deadline.format(fmt) + ")";
    }
}
