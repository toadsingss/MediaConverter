import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import java.io.File;

public class Main extends Application {

    // --- Variables globales pour l'UI ---
    private TextField fileText;
    private ComboBox<String> formatBox;
    private ProgressBar progressBar;
    private Label statusLabel;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Convertisseur multimédia");

        // --- TITRE ---
        Label title = new Label("Convertisseur multimédia");
        title.getStyleClass().add("title");
        Label subtitle = new Label("Choisissez un fichier et un format de sortie");
        subtitle.getStyleClass().add("subtitle");
        VBox header = new VBox(6, title, subtitle);
        header.setAlignment(Pos.CENTER);

        // --- CHAMP TEXTE + BOUTON PARCOURIR ---
        fileText = new TextField();
        fileText.setPromptText("Aucun fichier sélectionné");
        fileText.getStyleClass().add("text-field");

        Button browse = new Button("📂 Parcourir");
        browse.getStyleClass().addAll("btn", "btn-secondary");
        browse.setOnAction(e -> chooseFile(stage));

        HBox fileRow = new HBox(10, fileText, browse);
        fileRow.setAlignment(Pos.CENTER);

        // --- COMBOBOX FORMAT ---
        formatBox = new ComboBox<>();
        formatBox.getItems().addAll("mp3", "m4a", "wav", "mp4", "avi");
        formatBox.getSelectionModel().selectFirst();
        formatBox.getStyleClass().add("combo");

        Label formatLabel = new Label("Format de sortie :");
        formatLabel.getStyleClass().add("label");

        HBox formatRow = new HBox(10, formatLabel, formatBox);
        formatRow.setAlignment(Pos.CENTER);

        // --- BOUTONS ACTION ---
        Button convert = new Button("⚡ Convertir");
        convert.getStyleClass().addAll("btn", "btn-primary");
        convert.setOnAction(e -> startConversion());

        Button clear = new Button("✖ Effacer");
        clear.getStyleClass().addAll("btn", "btn-ghost");
        clear.setOnAction(e -> {
            fileText.clear();
            status("Prêt");
        });

        HBox actions = new HBox(10, convert, clear);
        actions.setAlignment(Pos.CENTER);

        // --- FEEDBACK (BARRE + LABEL) ---
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(260);
        progressBar.setVisible(false);

        statusLabel = new Label("Prêt");
        statusLabel.getStyleClass().add("muted");

        VBox feedback = new VBox(12, statusLabel, progressBar);
        feedback.setAlignment(Pos.CENTER);

        // --- CONTAINER PRINCIPAL ---
        VBox card = new VBox(16, header, fileRow, formatRow, actions, feedback);
        card.setAlignment(Pos.CENTER); // centre tous les enfants
        card.setPadding(new Insets(18));
        card.getStyleClass().add("card");

        VBox root = new VBox(20, card);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));

        // --- SCENE ---
        Scene scene = new Scene(root, 640, 360);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setScene(scene); // ⚠️ attention à bien utiliser setScene !
        stage.show();

        // --- ANIMATION D'APPARITION ---
        FadeTransition ft = new FadeTransition(Duration.millis(400), card);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    // --- MÉTHODE POUR OUVRIR LE FILE CHOOSER ---
    private void chooseFile(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Sélectionner un fichier");
        File f = fc.showOpenDialog(stage);
        if (f != null) fileText.setText(f.getAbsolutePath());
    }

    // --- MÉTHODE POUR LANCER LA CONVERSION ---
    private void startConversion() {
        String input = fileText.getText();
        if (input == null || input.isBlank()) {
            alert(Alert.AlertType.ERROR, "Veuillez choisir un fichier.");
            return;
        }

        String fmt = formatBox.getValue();
        String output = input.substring(0, input.lastIndexOf('.')) + "." + fmt;

        progressBar.setVisible(true);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        status("Conversion en cours…");

        // --- THREAD POUR NE PAS BLOQUER L'UI ---
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-y", "-i", input, output);
                Process p = pb.start();
                int code = p.waitFor();

                javafx.application.Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    if (code == 0) {
                        status("✅ Fichier créé : " + output);
                        alert(Alert.AlertType.INFORMATION, "Conversion terminée !\n" + output);
                    } else {
                        status("❌ Erreur FFmpeg (code " + code + ")");
                        alert(Alert.AlertType.ERROR, "La conversion a échoué (code " + code + ")");
                    }
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    status("❌ " + ex.getMessage());
                    alert(Alert.AlertType.ERROR, ex.getMessage());
                });
            }
        }).start();
    }

    // --- MÉTHODE POUR CHANGER LE TEXTE DU STATUT ---
    private void status(String s) {
        statusLabel.setText(s);
    }

    // --- MÉTHODE POUR LES ALERTES ---
    private void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setTitle(type == Alert.AlertType.ERROR ? "Erreur" : "Information");
        a.setContentText(msg);
        a.show();
    }

    // --- POINT D'ENTRÉE ---
    public static void main(String[] args) {
        launch(args);
    }
}
