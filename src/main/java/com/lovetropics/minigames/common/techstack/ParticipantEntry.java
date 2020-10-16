package com.lovetropics.minigames.common.techstack;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ParticipantEntry {
    private static final String[] FAKE_NAMES = new String[]{"Cojo", "tterrag", "Lune", "Brick", "Terry"};
    private static final String[] FAKE_UNITS = new String[]{"points", "seconds", "kills"};

    /* Minecraft username */
    private String name;
    /** 1, 2, 3..idk, what place did YOU finish in? */
    private Integer place;
    /** Ex: 3:00, 10000, etc */
    private String score;
    /** Ex: points, seconds, etc */
    private String units;

    public ParticipantEntry() {
    }

    public ParticipantEntry(final String name, final Integer place, final String score, final String units) {
        this.name = name;
        this.place = place;
        this.score = score;
        this.units = units;
    }

    public static List<ParticipantEntry> fakeEntries() {
        final Random random = new Random();
        final int numParticipants = random.nextInt(4) + 1;

        List<String> names = Arrays.asList(FAKE_NAMES);
        Collections.shuffle(names);

        final List<ParticipantEntry> entries = Lists.newArrayList();

        for (int i = 0; i < numParticipants; i++) {
            entries.add(new ParticipantEntry(names.get(i), i, "" + random.nextInt(1000), FAKE_UNITS[random.nextInt(FAKE_UNITS.length)]));
        }

        return entries;
    }
}
