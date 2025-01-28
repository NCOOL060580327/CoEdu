package kdt.web_ide.boards.entity;

import jakarta.persistence.*;
import kdt.web_ide.BaseTimeEntity;
import kdt.web_ide.post.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "boards")
public class Board extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Builder.Default
    @Column(name = "user_count")
    private int userCount = 1;

    @Column(name = "title_text")
    private String titleText;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    public void update(String title,String titleText){
        this.title = title;
        this.titleText = titleText;
    }

}
