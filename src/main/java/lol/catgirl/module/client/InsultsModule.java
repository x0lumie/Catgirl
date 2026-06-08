package lol.catgirl.module.client;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.EntityRemovedEvent;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.Module;
import lol.catgirl.property.impl.EnumProperty;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class InsultsModule extends Module {

    public static final InsultsModule INSTANCE = new InsultsModule();

    public enum Mode {
        Removal, Texts
    }

    public enum InsultsMode {
        Catgirl, Lumie, Custom, Islam
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Removal);
    public final EnumProperty<InsultsMode> insults = new EnumProperty<>("Insults mode", InsultsMode.Catgirl);

    public InsultsModule() {
        super("Insults",
                "Automatically insults players when you kill them.",
                ModuleCategory.Client
        );
        addSettings(mode, insults);
    }

    private final Random random = new Random();
    public Entity currentTarget;
    public String lastVictimName;

    @EventHook
    public void onPacket(PacketReceivedEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (!(event.packet instanceof ClientboundSystemChatPacket packet)) return;
        if (mode.getValue() != Mode.Texts) return;
        if(!this.isEnabled()) return;

        String[] killWords = {
                "Eliminating a player",
                "was slain by " + mc.player.getScoreboardName(),
                "was killed by " + mc.player.getScoreboardName(),
                "has been killed by " + mc.player.getScoreboardName(),
                "You killed ",
                "killed by " + mc.player.getScoreboardName(),
                "void by " + mc.player.getScoreboardName(),
                "slain by " + mc.player.getScoreboardName(),
                "void while escaping " + mc.player.getScoreboardName(),
                "was killed with magic while fighting " + mc.player.getScoreboardName(),
                "couldn't fly while escaping " + mc.player.getScoreboardName(),
                "fell to their death while escaping " + mc.player.getScoreboardName()
        };

        String message = packet.content().getString();

        for (String keywords : killWords) {
            if (message.contains(keywords)) {
                lastVictimName = message.split(" ")[1];

                List<String> list = List.copyOf(getInsults());

                String insultMsg = list.get(random.nextInt(list.size()));
                mc.player.connection.sendChat(insultMsg);
            }
        }
    }

    @EventHook
    public void onRemoved(EntityRemovedEvent event) {
        if(!this.isEnabled()) return;

        if (mode.getValue() == Mode.Removal) {
            if (mc.player == null) return;
            Entity entity = event.entity;

            if (!(entity instanceof Player victim)) return;
            if (victim == mc.player) return;

            if (victim.getLastDamageSource() == null) return;
            if (victim.getLastDamageSource().getEntity() != mc.player) return;

            currentTarget = victim;

            List<String> list = List.copyOf(getInsults());

            String message = list.get(random.nextInt(list.size()));
            mc.player.connection.sendChat(message);
        }
    }

    private String getVictim() {
        return currentTarget != null ? currentTarget.getName().getString() : (lastVictimName != null ? lastVictimName : "someone");
    }


    private List<String> getInsults() {
        switch (insults.getValue()) {

            case Catgirl -> {
                return List.of(
                        "Why waste another game without Catgirl?",
                        "It is a true fact that when you use Catgirl Client the chance of" +
                                " getting a Catgirl goes up by 99%!",
                        "Charlie Kirk was shot by Catgirl Client's KillAura!",
                        "Charlie Kirk gives Catgirl Client 5 stars all around!",
                        "Cool man the sex man cannot take the powa of Catgirl Client's Aura.",
                        "Jpx3 are you are you a fed collecting telemetry to patch my bypasses?",
                        "where is my up button lol",
                        "u wanna talk shit to my sister? i dont think so, i fk my sister buddy boy.."
                );
            }

            case Lumie -> {
                return List.of(

                        "\"Cool man the sex man\" Awesome, so when did you lose" +
                        " your virginity? you do realize that's illegal " +
                        "because you aren't over the age of 18, right? " +
                        "Sorry, I'll correct myself. It is, by law, " +
                        "legal to have sexual intercourse once both consenting " +
                        "participants are above the legal age of consent, which in " +
                        "most cases is 16 to 17. Based on your immaturity regarding your name," +
                        " and commend, and lack of grammar + punctuation, and lack of basic human" +
                        " respect towards others. I seriously doubt that you are 16 or 17, much " +
                        "less 18 or above. So, I would recommend that you do some investigating " +
                        "inside of yourself. Gosh. I can't think of the term... Oh! It's uhm... hm." +
                        " how about you find God. Because clearly you fucking need it, you pathetic" +
                        " delinquent. I'd be surprised if you've even read this far honestly. What, " +
                        "gonna reply with, and i quote \"i ain't readin allat.\" Cool, go sky dive " +
                        "off of a building with a drop that is fatal. Don't bother bringing a " +
                        "parachute, this world would be a much better place if you dove without one." +
                        " \"fake smile ahh art\" Shit, you make me sick.",

                        "maybe i want u to put ur hands where u want to :3",
                        "imagine getting killed by a client thats pasted from rise",
                        "bald red man wants u to go get simp @github/x0lumie/Simp",
                        "my dog itches so can u like itch it for me",
                        "\"best aac bypasses!!1!1\" like bro stfu ur client is so donkey dooks",
                        "how wood would alan wood suck if alan wood could suck wood?",
                        "ouija board vs 25 woke students. im charlie kirk back from the dead mf",
                        "i bet u love receiving backshots from KotlinProject",
                        "i paste them astolfo scripts like its ur moms first taste of my dih.",
                        "rawr xD x3 nuzzles u UwU",
                        "womp womp",
                        "sniped by ducky $$ get my client @github/x0lumie/Simp",
                        "polar pop bypass $$",
                        "go back to 2022 skid #famous",
                        "it's my b-day. b nice 2 me :<",
                        "bombies is my little $1utt",
                        "\"i'm not a furry but i do like to be called daddy uwu\"",
                        "i knew some1 tht said he would let bombies stack donuts on it.." +
                                " i agree."
                );
            }

            case Islam -> {
                return List.of("muhammad was a caravan robber",
                        "muhammad married a 9 year old",
                        "muhammad is a fucking pedo",
                        "fun fact: aisha used to scrape semen stains off the prophets clothes!",
                        "fuck your moon god!", "muhammad is a retarded false prophet",
                        "allah cant save you from me rsmwahahaha", "islam is a crime against humanity",
                        "not even allah can move as fast as me", "go drink some fucking camel urine you retard",
                        "fuck muhammad", "muhammad piss be upon him", "leave fucking islam",
                        "the quran isnt preserved you fucktard", "u need Jesus not some pedophile prophet",
                        "astaghfirullah! theres semen on my clothes!!!!",
                        "stop idolizing fucking zakir naik and grow a brain retard",
                        "let me get this straight, muhammad was a pedo and hes supposed to be the best example for humanity? XD",
                        "bro was doing wudu afk",
                        "i am fighting for the cause of allah for my 72 virgins in paradise!!!!",
                        "fuck your religion and fuck your prophet",
                        "muslims kill people in a block game to prepare for jihad",
                        "islam was spread by the sword",
                        "your dad prays to a black rock",
                        "the quran is good quality toilet paper - 5 stars",
                        "islam is cancer",
                        "your fucking desert culture is a joke, go back to riding camels",
                        "you fucking muslim terrorist",
                        "ur mom wears a burqa and still cant hide her shame",
                        "the quran says allah sends people to hell for not fasting during ramadan you fatass"
                );
            }

            case Custom -> {
                try {
                    File dir = new File(mc.gameDirectory, "Catgirl");

                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    File file = new File(dir, "Insults.txt");

                    if (!file.exists()) {
                        file.createNewFile();

                        try (FileWriter writer = new FileWriter(file)) {
                            writer.write("1st insult\n");
                            writer.write("2nd insult\n");
                            writer.write("3rd insult");
                        }
                    }

                    List<String> insultsList = new ArrayList<>();

                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;

                        while ((line = reader.readLine()) != null) {
                            if (!line.trim().isEmpty()) {
                                insultsList.add(line);
                            }
                        }
                    }

                    if (insultsList.isEmpty()) {
                        insultsList.add("No insults found in insults.txt");
                    }

                    return insultsList;

                } catch (Exception e) {
                    e.printStackTrace();

                    return List.of("Failed to read insults.txt");
                }
            }
        }

        return List.of("Equinox Client");
    }
}
