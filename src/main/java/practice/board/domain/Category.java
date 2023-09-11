package practice.board.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.*;
import static lombok.AccessLevel.*;

@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Getter
@Setter(value = PRIVATE)
@Builder
@Entity
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @NotNull
    @Column(length = 30)
    private String name;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent", cascade = ALL)  //부모 카테고리 삭제 시, 자식 카테고리 자동 삭제됨
    private List<Category> child = new ArrayList<>();

    //TODO 카테고리에 포함된 글 목록 필드로 만들 필요 없을까?



    //== 연관관계 메서드 ==//
    public void addParent(Category parent) {
        this.setParent(parent);
        parent.getChild().add(this);
    }


    //== 생성 메서드 ==//
    public static Category createCategory(String name, Category parent) {
        Category category = Category.builder()
                .name(name)
                .build();

        category.addParent(parent);

        return category;
    }






}
