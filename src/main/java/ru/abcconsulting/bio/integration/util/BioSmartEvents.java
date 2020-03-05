package ru.abcconsulting.bio.integration.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public enum  BioSmartEvents {

    // OPEN_SHIFT
    EMPLOYEE_ENTRY (151L, "╨Т╤Е╨╛╨┤ ╤Б╨╛╤В╤А╤Г╨┤╨╜╨╕╨║╨░"),
    EMPLOYEE_ENTRY_BY_CARD (153L, "╨Т╤Е╨╛╨┤ ╤Б╨╛╤В╤А╤Г╨┤╨╜╨╕╨║╨░ (╨┐╨╛ ╨║╨░╤А╤В╨╡)"),
    EMPLOYEE_ENTRY_BY_PIN (156L, "╨Т╤Е╨╛╨┤ ╤Б╨╛╤В╤А╤Г╨┤╨╜╨╕╨║╨░ (╨┐╨╛ ╨┐╨╕╨╜-╨║╨╛╨┤╤Г)"),
    EMPLOYEE_ENTRY_MANUAL_INPUT(20L, "╨Т╤Е╨╛╨┤ (╤А╤Г╤З╨╜╨╛╨╣ ╨▓╨▓╨╛╨┤)"),

    // CLOSE_SHIFT
    EMPLOYEE_EXIT (152L,"╨Т╤Л╤Е╨╛╨┤ ╤Б╨╛╤В╤А╤Г╨┤╨╜╨╕╨║╨░"),
    EMPLOYEE_EXIT_BY_CARD (154L, "╨Т╤Л╤Е╨╛╨┤ ╤Б╨╛╤В╤А╤Г╨┤╨╜╨╕╨║╨░ (╨┐╨╛ ╨║╨░╤А╤В╨╡)"),
    EMPLOYEE_EXIT_BY_PIN (157L, "╨Т╤Л╤Е╨╛╨┤ ╤Б╨╛╤В╤А╤Г╨┤╨╜╨╕╨║╨░ (╨┐╨╛ ╨┐╨╕╨╜-╨║╨╛╨┤╤Г)"),
    EMPLOYEE_EXIT_MANUAL_INPUT( 21L, "╨Т╤Л╤Е╨╛╨┤ (╤А╤Г╤З╨╜╨╛╨╣ ╨▓╨▓╨╛╨┤)"),

    // OPEN_BREAK
    LUNCH_OUT (202L, "╨Т╤Л╤Е╨╛╨┤ ╨╜╨░ ╨╛╨▒╨╡╨┤"),
    LUNCH_OUT_BY_CARD (353L, "╨Т╤Л╤Е╨╛╨┤ ╨╜╨░ ╨╛╨▒╨╡╨┤ (╨┐╨╛ ╨║╨░╤А╤В╨╡)"),
    LUNCH_OUT_BY_PIN (355L, "╨Т╤Л╤Е╨╛╨┤ ╨╜╨░ ╨╛╨▒╨╡╨┤ (╨┐╨╛ ╨┐╨╕╨╜-╨║╨╛╨┤╤Г)"),

    // CLOSE_BREAK
    ENTRANCE_FROM_LUNCH (203L, "╨Т╤Е╨╛╨┤ ╤Б ╨╛╨▒╨╡╨┤╨░"),
    ENTRANCE_FROM_LUNCH_BY_CARD (354L, "╨Т╤Е╨╛╨┤ ╤Б ╨╛╨▒╨╡╨┤╨░ (╨┐╨╛ ╨║╨░╤А╤В╨╡)"),
    ENTRANCE_FROM_LUNCH_BY_PIN (356L, "╨Т╤Е╨╛╨┤ ╤Б ╨╛╨▒╨╡╨┤╨░ (╨┐╨╛ ╨┐╨╕╨╜-╨║╨╛╨┤╤Г)");



    private final Long eventId;
    private final String eventName;

    BioSmartEvents(Long eventId, String eventName) {
        this.eventId = eventId;
        this.eventName = eventName;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public static String getAllEventIdsInString() {
        return Arrays
                .stream(BioSmartEvents.values())
                .map(BioSmartEvents::getEventId)
                .map(Objects::toString)
                .collect(Collectors.joining(","));
    }
}
