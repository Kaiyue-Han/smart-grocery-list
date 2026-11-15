package xtasks.backend.entities;

import javax.persistence.*;

/** Successfully imported CSV contents, independent of the original filename. */
@Entity
@Table(name = "imported_files")
public class ImportedFile {
    @Id
    @Column(length = 64, nullable = false)
    private String digest;
    protected ImportedFile() {}
    public ImportedFile(String digest) { this.digest = digest; }
}
