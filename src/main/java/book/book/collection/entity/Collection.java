package book.book.collection.entity;

import book.book.collection.dto.UpdateCollectionRequest;
import book.book.common.BaseTimeEntity;
import book.book.common.CustomException;
import book.book.common.ResultCode;
import book.book.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Collection extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String name;
    private String description;

    public Collection(Member member, String name, String description) {
        this.member = member;
        this.name = name;
        this.description = description;
    }

    public void validateOwner(Long memberId) {
        if(!this.member.isSameById(memberId))
            throw new CustomException(ResultCode.IS_NOT_OWNER);
    }

    public void update(UpdateCollectionRequest request) {
        if(request.getName() != null)
            this.name = request.getName();
        if(request.getDescription() != null)
            this.description = request.getDescription();
    }
}