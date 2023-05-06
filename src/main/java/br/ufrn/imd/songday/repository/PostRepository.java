package br.ufrn.imd.songday.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import br.ufrn.imd.songday.dto.post.PostSearchDto;
import br.ufrn.imd.songday.model.Post;

public interface PostRepository extends MongoRepository<Post, String> {
    boolean existsByUserIdAndCreatedAtBetween(String userId, Date start, Date end);

    @Aggregation(pipeline = {
        "{$match: {userId: {$in: ?0}}}",
        "{$sort: {createdAt: -1}}",
        "{$skip: ?1}",
        "{$limit: ?2}",
        "{$addFields: {userIdObject: {$toObjectId: '$userId'}}}",
        "{$lookup: {from: 'users', localField: 'userIdObject', foreignField: '_id', as: 'users'}}",
        "{$addFields: {user: {$arrayElemAt: ['$users', 0]}}}",
        "{$lookup: {from: 'comments', localField: '_id', foreignField: 'postId', as: 'comments'}}",
        "{$addFields: {commentsCount: {$size: '$comments'}}}",
        "{$addFields: {likesCount: {$size: '$userLikes'}}}",
    })
    List<PostSearchDto> findPosts(List<String> followees, int offset, int limit);
}
