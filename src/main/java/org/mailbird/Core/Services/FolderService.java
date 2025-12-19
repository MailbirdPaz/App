package org.mailbird.Core.Services;

import lombok.Setter;
import org.mailbird.Core.DAO.FolderDAO;
import org.mailbird.Core.domain.entity.FolderEntity;
import org.mailbird.Core.domain.entity.UserEntity;

import java.util.List;

public class FolderService {
    @Setter
    public String currentFolder = "INBOX";

    private FolderDAO folderDAO;

    public FolderService(FolderDAO folderDAO) {
        this.folderDAO = folderDAO;
    }

    public void CreateFolder(String folderName, UserEntity currentUser) {
        folderDAO.Create(folderName, currentUser);
    }

    public List<FolderEntity> GetAllFolders(UserEntity currentUser) {
        return folderDAO.GetByOwner(currentUser);
    }

    public void CreateDefaultFolder(UserEntity currentUser) {
        List<FolderEntity> folders = GetAllFolders(currentUser);

        // stream - 'for' cycle short analog
        boolean inboxExists = folders.stream()
                .anyMatch(f -> "INBOX".equalsIgnoreCase(f.getTitle()));

        if (!inboxExists) {
            CreateFolder("INBOX", currentUser);
        }
    }

    public FolderEntity GetCurrentFolderEntity(UserEntity currentUser) {
        return folderDAO.GetByOwnerAndTitle(currentUser, currentFolder);
    }
}
