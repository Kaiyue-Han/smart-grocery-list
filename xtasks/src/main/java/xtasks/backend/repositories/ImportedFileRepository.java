package xtasks.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import xtasks.backend.entities.ImportedFile;

public interface ImportedFileRepository extends JpaRepository<ImportedFile, String> {}
