package cvguess.core;

import cvguess.model.Category;
import cvguess.model.Leaderboard;
import cvguess.model.Player;

import java.awt.image.BufferedImage;

public class GameController {

    public static class RoundState {
        public final BufferedImage shownImage;
        public final int timeLeft;
        public final int correctCount;
        public final int wrongTries;
        public final String debugFileName; // istersen görünmesin diye UI’da kullanma

        public RoundState(BufferedImage shownImage, int timeLeft, int correctCount, int wrongTries, String debugFileName) {
            this.shownImage = shownImage;
            this.timeLeft = timeLeft;
            this.correctCount = correctCount;
            this.wrongTries = wrongTries;
            this.debugFileName = debugFileName;
        }
    }

    private final ImageRepository repo;
    private final ImageProcessor processor = new ImageProcessor();
    private final PixelBlockGenerator generator = new PixelBlockGenerator();
    private final Leaderboard leaderboard;

    private Player player;
    private Category category;

    private int timeLeft = 30;
    private int wrongTries = 0;

    private ImageRepository.ImageItem current;

    // Parametreler (zorluk)
    private static final int TARGET_SIZE = 360;

    public GameController(ImageRepository repo, Leaderboard leaderboard) {
        this.repo = repo;
        this.leaderboard = leaderboard;
    }

    public void startNewGame(String playerName, Category category) {
        this.player = new Player(playerName);
        this.category = category;
        this.timeLeft = 60; // 30 -> 60 saniye
        this.wrongTries = 0;
        nextImage();
        
        if (current == null) {
            throw new RuntimeException("Görsel yüklenemedi! Lütfen 'images' klasörünü kontrol edin.");
        }
    }

    public RoundState getCurrentState() {
        BufferedImage shown = buildShownImage();
        return new RoundState(shown, timeLeft, player.getCorrectCount(), wrongTries,
                current != null ? current.rawName : "");
    }

    public boolean isGameOver() {
        return timeLeft <= 0;
    }

    public void tickOneSecond() {
        timeLeft--;
    }

    public enum GuessResult { CORRECT, WRONG, GAME_OVER }

    public GuessResult submitGuess(String guessRaw) {
        if (current == null) return GuessResult.GAME_OVER;
        if (timeLeft <= 0) return GuessResult.GAME_OVER;

        String g = ImageRepository.normalizeAnswer(guessRaw);
        if (g.isEmpty()) return GuessResult.WRONG;

        boolean ok = isMatch(g, current.answer);
        if (ok) {
            player.incrementCorrect();
            timeLeft += 3;
            wrongTries = 0;
            nextImage();
            return GuessResult.CORRECT;
        } else {
            timeLeft -= 2;
            wrongTries++;
            if (timeLeft <= 0) return GuessResult.GAME_OVER;
            return GuessResult.WRONG;
        }
    }

    private boolean isMatch(String guess, String answer) {
        // Basit eşleşme: tam eşit veya “answer” kelimesini içeriyor
        if (guess.equals(answer)) return true;
        if (guess.contains(answer)) return true;
        if (answer.contains(guess) && guess.length() >= 3) return true;
        return false;
    }

    private void nextImage() {
        for (int i = 0; i < 10; i++) {
            current = repo.randomItem(category);
            if (current != null) return;
            System.out.println("Failed to load image, retrying... (" + (i + 1) + "/10)");
        }
        System.err.println("CRITICAL: Could not load any image after 10 tries!");
        // current remains null, but we should probably handle this in UI
    }

    private BufferedImage buildShownImage() {
        if (current == null) {
            System.out.println("Current image is null! Returning placeholder.");
            return new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        }

        BufferedImage base = processor.centerCropAndResize(current.image, TARGET_SIZE);

        // Zorluk: wrongTries arttıkça detay aç
        // HIZLANDIRILMIŞ VERSİYON
        boolean twoBlocks = (wrongTries == 0);
        
        // Grid daha hızlı büyüsün: 2 -> 4 -> 6 -> 8...
        int gridN = Math.min(20, 2 + wrongTries * 2); 
        
        // Pixelation daha hızlı azalsın: 12 -> 8 -> 4 -> 1 (net)
        int pixelFactor = Math.max(1, 12 - wrongTries * 4); 
        
        // Renkler daha hızlı açılsın
        int colorLevels = Math.min(64, 8 + wrongTries * 8);

        BufferedImage quant = processor.quantizeColors(base, colorLevels);
        return generator.generate(quant, gridN, twoBlocks, pixelFactor);
    }

    public void finalizeAndSave() {
        if (player == null) return;
        player.setFinalTimeSeconds(Math.max(0, timeLeft));
        leaderboard.add(player.getName(), player.getCorrectCount(), player.getFinalTimeSeconds());
    }

    public Player getPlayer() { return player; }
    public int getTimeLeft() { return timeLeft; }
    
    public String getCurrentAnswer() {
        return current != null ? current.answer : "???";
    }
    
    public BufferedImage getCurrentImage() {
        return current != null ? current.image : null;
    }
}
