package duke;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;


public class Parser {
    private final TaskList tasks;
    private final Storage storage;

    private final DateTimeFormatter fmt = new DateTimeFormatterBuilder()
            .appendPattern("d/M/yyyy")
            .optionalStart()
            .appendPattern(" HHmm")
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter();

    Parser(TaskList tasks, Storage storage) {
        this.tasks = tasks;
        this.storage = storage;
    }

    /**
     * Returns true if input string is "bye"
     *
     * @param input Received string from scanner
     * @return Boolean based on checking if input is "bye"
     */
    public boolean isExit(String input) {
        return input.equals("bye");
    }


    /**
     * Parses the input given and returns the responses given the input.
     *
     * @param input Received string from scanner
     * @return Response from program.
     */
    public String[] parseInput(String input) {
        try {
            if (input.startsWith("done")) {
                return new String[]{setTaskAsDone(input)};
            } else if (input.startsWith("delete")) {
                return new String[]{deleteTask(input)};
            } else if (input.startsWith("list")) {
                return list();
            } else if (input.startsWith("find")) {
                return find(input);
            } else if (input.startsWith("snooze")) {
                return snooze(input);
            } else {
                return addTask(input);
            }
        } catch (DukeException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String[] snooze(String input) throws IOException {
        //format: snooze 1 /to 2/2/2022 2222
        String[] splitString = input.split(" ", 2);
        if (splitString.length == 1) {
            return new String[]{"OOPS!!! You have not inputted a task or a rescheduled time"};
        }
        String[] splitSnooze = splitString[1].split("/to", 2);
        int taskIndex = Integer.parseInt(splitSnooze[0]);
        Task task = tasks.get(taskIndex - 1);
        LocalDateTime newDate = LocalDateTime.parse(splitSnooze[1], fmt);



        return new String[]{};
    }

    private String[] addTask(String input) throws DukeException, IOException {
        String[] splitString = input.split(" ", 2);
        String type = splitString[0];
        Task task;
        switch (type) {
        case "todo":
            if (splitString.length == 1) {
                return new String[]{"OOPS!!! The description of a todo cannot be empty.\n"};
            }
            task = new Todo(splitString[1]);
            break;
        case "deadline":
            if (splitString.length == 1) {
                return new String[]{"OOPS!!! The description of a deadline cannot be empty.\n"};
            }
            String[] splitDeadline = splitString[1].split(" /by ", 2);
            if (splitDeadline.length == 1) {
                return new String[]{"OOPS!!! The description or deadline can't be empty or it must be after a '/'"};
            }
            task = new Deadline(splitDeadline[0], LocalDateTime.parse(splitDeadline[1], fmt));
            break;
        case "event":
            if (splitString.length == 1) {
                return new String[]{"OOPS! The description of an event cannot be empty.\n"};
            }
            String[] splitEvent = splitString[1].split(" /at ", 2);
            if (splitEvent.length == 1) {
                return new String[]{"\tOOPS!!! The description or duration can't be empty or it must be after a '/'"};
            }
            task = new Event(splitEvent[0], LocalDateTime.parse(splitEvent[1], fmt));
            break;
        default:
            return new String[]{"OOPS!!! I'm sorry, but I don't know what that means :-("};
        }
        tasks.add(task);
        storage.write(task.save());
        return displayTasks();
    }
    private String[] displayTasks() {
        ArrayList<String> results = new ArrayList<>();
        results.add("\tGot it. I've added this task:\n\t\t" + tasks.get(tasks.size() - 1).toString()
                + "\n\tNow you have " + tasks.size() + " tasks in the list.");
        return results.toArray(new String[0]);
    }
    private String deleteTask(String input) {
        String[] splitString = input.split(" ", 2);
        int index = Integer.parseInt(splitString[1]) - 1;
        Task removedTask;
        if (index > tasks.size() + 1) {
            return "OOPS!!! The task doesn't exist!\n";
        } else {
            removedTask = tasks.remove(index);
        }
        assert removedTask != null : "Task removed must exist!";
        try {
            storage.writeEntireFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "\tNoted. I've removed this task:\n" + "\t\t" + removedTask.toString()
                + "\n" + "\tNow you have " + tasks.size() + " tasks in the list.";
    }

    private String setTaskAsDone(String input) throws DukeException {
        String[] splitString = input.split(" ", 2);
        int i = Integer.parseInt(splitString[1]) - 1;
        if (i + 1 <= 0 || i + 1 > tasks.size()) {
            throw new DukeException("Task not found!");
        }
        tasks.get(i).markAsDone();
        try {
            storage.writeEntireFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "\tNice! I've marked this task as done:\n\t\t" + tasks.get(i).toString();
    }

    private String[] list() {
        ArrayList<String> list = new ArrayList<>();
        list.add("\tHere are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            list.add("\t" + (i + 1) + ". " + tasks.get(i).toString());
        }
        return list.toArray(new String[0]);
    }

    private String[] find(String input) {
        String[] splitString = input.split(" ", 2);
        ArrayList<String> list = new ArrayList<>();
        list.add("\tHere are the matching tasks in your list:");
        if (splitString.length == 1) {
            for (int i = 0; i < tasks.size(); i++) {
                list.add("\t" + (i + 1) + ". " + tasks.get(i).toString());
            }
            return list.toArray(new String[0]);
        }
        String keyword = splitString[1];
        int currentIndex = 0;
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).toString().contains(keyword)) {
                list.add("\t" + (currentIndex + 1) + ". " + tasks.get(i).toString());
                currentIndex++;
            }
        }
        return list.toArray(new String[0]);
    }
}
