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
    private int userCount = 1;

    public void update(String title){
        this.title = title;
    }

}
