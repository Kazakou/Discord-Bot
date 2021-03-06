import net.dv8tion.jda.JDA;
import net.dv8tion.jda.audio.player.URLPlayer;
import net.dv8tion.jda.audio.player.Player;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

import com.github.axet.vget.VGet;

import javax.security.auth.login.LoginException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Main extends ListenerAdapter {
    private Player player = null;
    private String prefix = "/";

    public static void main(String[] args) {
        try {
            @SuppressWarnings("unused")
			JDA api = new JDABuilder()
                .setEmail("roberta.637657@nv.ccsd.net")
                .setPassword("pcel8bitch1")
                .addListener(new Main())
                .buildBlocking();
        } catch (IllegalArgumentException e) {
            System.out.println("The config was not populated. Please enter an email and password.");
            e.printStackTrace();
        } catch (LoginException e) {
            System.out.println("The provided email / password combination was incorrect. Please provide valid details.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("null")
	public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {
        String message = event.getMessage().getContent();

        if (message.indexOf(prefix) != 0) {
            return;
        }
        message = message.substring(1);

        //Start an audio connection with a VoiceChannel
        if (message.startsWith("join "))
        {
            //Separates the name of the channel so that we can search for it
            String chanName = message.substring(5);

            //Scans through the VoiceChannels in this Guild, looking for one with a case-insensitive matching name.
            VoiceChannel channel = event.getGuild().getVoiceChannels().stream().filter(
                    vChan -> vChan.getName().equalsIgnoreCase(chanName))
                .findFirst().orElse(null);  //If there isn't a matching name, return null.
            if (channel == null)
            {
                event.getChannel().sendMessage("There isn't a VoiceChannel in this Guild with the name: '" + chanName + "'");
                return;
            }
            event.getGuild().getAudioManager().closeAudioConnection();
            event.getGuild().getAudioManager().openAudioConnection(channel);
        }
        //Disconnect the audio connection with the VoiceChannel.
        if (message.equals("leave")) {
            event.getJDA().shutdown(false);
        }

        //Start playing audio with our FilePlayer. If we haven't created and registered a FilePlayer yet, do that.
        if (message.equals("play"))
        {
            //If the player didn't exist, create it and start playback.
            if (player == null)
            {
                File audioFile = null;
                URL audioUrl = null;
                try
                {
                	VGet v = new VGet(new URL("https://www.youtube.com/watch?v=p13lhwtSaQc"),
                			new File(System.getProperty("user.dir")));
                	v.download();
                	
                    //audioFile = new File("aac-41100.m4a");
                    audioUrl = new URL("http://k003.kiwi6.com/hotlink/5kkq64nc5y/Hip_Song.mp3");

                    //player = new FilePlayer(audioFile);
                    player = new URLPlayer(event.getJDA(), audioUrl);

                    //Provide the handler to send audio.
                    //NOTE: You don't have to set the handler each time you create an audio connection with the
                    // AudioManager. Handlers persist between audio connections. Furthermore, handler playback is also
                    // paused when a connection is severed (closeAudioConnection), however it would probably be better
                    // to pause the play back yourself before severing the connection (If you are using a player class
                    // you could just call the pause() method. Otherwise, make canProvide() return false).
                    // Once again, you don't HAVE to pause before severing an audio connection,
                    // but it probably would be good to do.
                    event.getGuild().getAudioManager().setSendingHandler(player);

                    //Start playback. This will only start after the AudioConnection has completely connected.
                    //NOTE: "completely connected" is not just joining the VoiceChannel. Think about when your Discord
                    // client joins a VoiceChannel. You appear in the channel lobby immediately, but it takes a few
                    // moments before you can start communicating.
                    player.setVolume(.15F);
                    player.play();
                }
                catch (IOException e)
                {
                    event.getChannel().sendMessage("Could not load the file. Does it exist?  File name: " + audioFile.getName());
                    e.printStackTrace();
                }
                catch (UnsupportedAudioFileException e)
                {
                    event.getChannel().sendMessage("Could not load file. It either isn't an audio file or isn't a" +
                        " recognized audio format.");
                    e.printStackTrace();
                }
            }
            else if (player.isStarted() && player.isStopped())  //If it did exist, has it been stop()'d before?
            {
                event.getChannel().sendMessage("The player has been stopped. To start playback, please use 'restart'");
                return;
            }
            else    //It exists and hasn't been stopped before, so play. Note: if it was already playing, this will have no effect.
            {
                player.play();
            }
        }

        if (message.startsWith("vol")) {
            String volume = message.substring(4);
            float vol = Float.parseFloat(volume)/100;
            player.setVolume(vol);
        }

        //You can't pause, stop or restart before a player has even been created!
        if (player == null && (message.equals("pause") || message.equals("stop") || message.equals("restart")))
        {
            event.getChannel().sendMessage("You need to 'play' before you can preform that command.");
            return;
        }

        if (player != null)
        {
            if (message.equals("pause"))
                player.pause();
            if (message.equals("stop"))
                player.stop();
            if (message.equals("restart"))
                player.restart();
        }
    }
}