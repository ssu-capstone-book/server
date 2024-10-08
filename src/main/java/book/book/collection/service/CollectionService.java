package book.book.collection.service;

import book.book.collection.dto.CollectionResponse;
import book.book.collection.dto.SaveCollectionRequest;
import book.book.collection.dto.UpdateCollectionRequest;
import book.book.collection.entity.Collection;
import book.book.collection.repository.CollectionRepository;
import book.book.common.CustomException;
import book.book.common.ResultCode;
import book.book.member.entity.Member;
import book.book.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final MemberRepository memberRepository;
    private final CollectionRepository collectionRepository;
    private final CollectionImageService collectionImageService;
    private final CollectionBookService collectionBookService;

    /**
     * 이름이 겹치면 안되는 비지니스 요구사항 때문에 저장과 업데이트를 분리했습니다
     */
    @Transactional
    public void saveCollection(Long memberId, SaveCollectionRequest rq) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);

        validateSameNameCollection(member, rq.getName());

        Collection collection = new Collection(member, rq.getName(), rq.getDescription());
        collectionRepository.save(collection);

        collectionBookService.saveCollectionBooks(collection, rq.getBookInfos());
    }

    /**
     * 1. deleteAll() 다음에 saveAll 할지
     * 2. 책 하나씩 올리 거나 삭제할 떄 API 호출할지
     *
     * 일단 1번 선택
     */
    @Transactional
    public CollectionResponse updateCollection(Long memberId, Long collectionId,
                                               UpdateCollectionRequest rq) {

        Member member = memberRepository.findByIdOrElseThrow(memberId);
        Collection collection = collectionRepository.findByIdOrElseThrow(collectionId);

        validateSameNameCollection(member, rq.getName());
        collection.validateOwner(memberId);

        collection.update(rq);
        collectionBookService.deleteAllByCollection(collection);
        collectionBookService.saveCollectionBooks(collection, rq.getBookInfos());

        List<String> images = collectionImageService.getBookCollectionThumbnail(rq.getBookInfos()); //TODO
        return CollectionResponse.of(collection, images);
    }


    @Transactional
    public void deleteCollection(Long memberId, Long collectionId) {
        Collection collection = collectionRepository.findByIdOrElseThrow(collectionId);

        collection.validateOwner(memberId);

        collectionBookService.deleteAllByCollection(collection);
        collectionRepository.delete(collection);
    }

    private void validateSameNameCollection(Member member, String name) {
        if (collectionRepository.existsByMemberAndName(member, name)) {
            throw new CustomException(ResultCode.BOOKCOLLECTION_ALREADY);
        }
    }
}
