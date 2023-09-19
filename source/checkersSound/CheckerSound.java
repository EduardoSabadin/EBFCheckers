package checkersSound;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;

import java.io.File;
import java.io.IOException;

public class CheckerSound {
    private File soundFile;
    private AudioInputStream audioIn;
    private Clip clip;

    public CheckerSound(String pathSoundFile) {
        try {
            this.soundFile = new File(pathSoundFile);
            this.audioIn = AudioSystem.getAudioInputStream(soundFile);
            this.clip = AudioSystem.getClip();
            this.clip.open(audioIn);
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        try {
            this.clip.start();
            this.clip.loop(Clip.LOOP_CONTINUOUSLY);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                };
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.clip.stop();
    }

}
