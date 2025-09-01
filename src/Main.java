import javafx.application.Application;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import java.io.File;

public class Main extends Application {

    private TextField fileText;
    private ComboBox<String> formatBox;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Convertisseur multimédia");

        // Champ fichier
        Label fileLabel = new Label("Fichier source :");
        fileText = new TextField();
        fileText.setPrefWidth(250);

        Button browseButton = new Button("📂 Parcourir");
        browseButton.setOnAction(e -> chooseFile(primaryStage));

        HBox fileRow = new HBox(10, fileLabel, fileText, browseButton);
        fileRow.setAlignment(Pos.CENTER);

        // Choix format
        Label formatLabel = new Label("Format de sortie :");
        formatBox = new ComboBox<>();
        formatBox.getItems().addAll("mp3", "wav", "mp4", "avi");
        formatBox.getSelectionModel().selectFirst();

        HBox formatRow = new HBox(10, formatLabel, formatBox);
        formatRow.setAlignment(Pos.CENTER);

        // Bouton convertir
        Button convertButton = new Button("⚡ Convertir");
        convertButton.setOnAction(e -> convertFile());

        // Layout principal
        VBox layout = new VBox(20, fileRow, formatRow, convertButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f4f4f4;");

        Scene scene = new Scene(layout, 500, 200);

        // Style global (CSS inline)
        scene.getStylesheets().add("style.css");

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Choisir un fichier
    private void chooseFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier");
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            fileText.setText(selectedFile.getAbsolutePath());
        }
    }

    // Conversion avec FFmpeg
    private void convertFile() {
        String inputPath = fileText.getText();
        String outputFormat = formatBox.getValue();

        if (inputPath.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un fichier !");
            return;
        }

        try {
            String outputPath = inputPath.substring(0, inputPath.lastIndexOf('.')) + "." + outputFormat;

            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-y", "-i", inputPath, outputPath);
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();

            showAlert(Alert.AlertType.INFORMATION, "Succès", "✅ Conversion terminée !\nFichier créé : " + outputPath);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "❌ " + e.getMessage());
        }
    }

    // Alertes jolies
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}