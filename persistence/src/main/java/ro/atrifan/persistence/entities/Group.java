package ro.atrifan.persistence.entities;

import javax.persistence.*;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
@Entity
@Table(name="Grup")
public class Group {

    @Id
    @GeneratedValue
    private long id;

    @Column(unique=true)
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
