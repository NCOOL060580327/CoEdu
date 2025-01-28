package kdt.web_ide.boards.entity;

import jakarta.persistence.*;
import kdt.web_ide.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public void update(String title,String titleText){
        this.title = title;
        this.titleText = titleText;
    }

}
