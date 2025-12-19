package org.mailbird.Core.Controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.mailbird.Core.Services.FolderService;
import org.mailbird.Core.domain.entity.UserEntity;

import java.util.function.Consumer;

public class FolderController {

    @FXML
    private Button button_new_folder;

    @FXML
    private TextField input_new_folder;

    @FXML
    private ListView<Text> list_folders;

    private FolderService folderService;
    private UserEntity user;
    private Consumer<String> onSelect;

    public FolderController(FolderService folderService, UserEntity user, Consumer<String> onSelect) {
        this.folderService = folderService;
        this.user = user;
        this.onSelect = onSelect;
    }

    @FXML
    void initialize() {
        loadFolders();

        button_new_folder.setOnAction(event -> {
            String folderTitle = input_new_folder.getText().trim();
            if (!folderTitle.isEmpty()) {
                this.folderService.CreateFolder(folderTitle, this.user);
                list_folders.getItems().add(new Text(folderTitle));
                input_new_folder.clear();
            }
        });

        // on folder select
        list_folders.setOnMouseClicked(e -> {
            String folder_name = list_folders.getSelectionModel().getSelectedItem().getText();
            if (folder_name != null && !folder_name.isEmpty()) {
                this.folderService.setCurrentFolder(folder_name);
                onSelect.accept(folder_name);
                closeWindow(e);
            }
        });
    }

    private void closeWindow(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();
        stage.close();
    }

    // set folders to the list view
    private void loadFolders() {
        this.folderService.GetAllFolders(this.user).forEach(folder -> {
            this.list_folders.getItems().add(new Text(folder.getTitle()));
        });
    }
}
