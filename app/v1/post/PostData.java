package v1.post;

import java.util.List;
import javax.persistence.*;

/**
 * Data returned from the database
 */
@Entity
@Table(name = "posts")
public class PostData {

    public PostData() {
    }

    public PostData(String userId, String urls) {
        this.userId = userId;
        this.urls = urls;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long id;
    public String userId;
    public String urls;
}
