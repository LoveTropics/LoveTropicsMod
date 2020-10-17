package com.lovetropics.minigames.common.techstack;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ParticipantEntry {
    public static final String UNIT_POINTS = "points";
    public static final String UNIT_SECONDS = "seconds";
    public static final String UNIT_KILLS = "kills";

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

    public static ParticipantEntry create(GameProfile profile, int place, String score, String units) {
        String name = profile != null ? profile.getName() : "Herobrine";
        return new ParticipantEntry(name, place, score, units);
    }

    public static ParticipantEntry withKills(GameProfile profile, int place, int kills) {
        return ParticipantEntry.create(profile, place, String.valueOf(kills), UNIT_KILLS);
    }

    public static ParticipantEntry withPoints(GameProfile profile,  int place, int points) {
        return ParticipantEntry.create(profile, place, String.valueOf(points), UNIT_POINTS);
    }

    public static ParticipantEntry withSeconds(GameProfile profile, int place, int seconds) {
        return ParticipantEntry.create(profile, place, String.valueOf(seconds), UNIT_SECONDS);
    }

    public String getName() {
        return name;
    }

    public Integer getPlace() {
        return place;
    }

    public String getScore() {
        return score;
    }

    public String getUnits() {
        return units;
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
