import axios from 'axios';
import { useState, useEffect } from 'react';
import './Posts.css';

function Posts() {
    const [content, setContent] = useState('');
    const [posts, setPosts] = useState([]);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [commentContent, setCommentContent] = useState({});
    const [tagContent, setTagContent] = useState({});

    const fetchPosts = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/posts', {
                withCredentials: true
            });
            setPosts(response.data);
        } catch (error) {
            setError('Failed to load posts');
        }
    };

    const fetchComments = async (postId) => {
        try {
            const response = await axios.get(`http://localhost:8080/api/comments/post/${postId}`, {
                withCredentials: true
            });
            setPosts(prevPosts => prevPosts.map(post =>
                post.id === postId ? { ...post, comments: response.data } : post
            ));
        } catch (error) {
            setError('Failed to load comments');
        }
    };

    const fetchLikes = async (postId) => {
        try {
            const response = await axios.get(`http://localhost:8080/api/likes/post/${postId}`, {
                withCredentials: true
            });
            setPosts(prevPosts => prevPosts.map(post =>
                post.id === postId ? { ...post, likes: response.data } : post
            ));
        } catch (error) {
            setError('Failed to load likes');
        }
    };

    const fetchTags = async (postId) => {
        try {
            const response = await axios.get(`http://localhost:8080/api/tags/post/${postId}`, {
                withCredentials: true
            });
            setPosts(prevPosts => prevPosts.map(post =>
                post.id === postId ? { ...post, tags: response.data } : post
            ));
        } catch (error) {
            setError('Failed to load tags');
        }
    };

    const handleCreatePost = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        try {
            const response = await axios.post('http://localhost:8080/api/posts',
                { content },
                { withCredentials: true }
            );
            setSuccess(response.data);
            setContent('');
            fetchPosts();
        } catch (error) {
            setError(error.response?.data || 'Failed to create post');
        }
    };

    const handleCreateComment = async (e, postId) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        try {
            const response = await axios.post('http://localhost:8080/api/comments',
                { postId, content: commentContent[postId] || '' },
                { withCredentials: true }
            );
            setSuccess(response.data);
            setCommentContent(prev => ({ ...prev, [postId]: '' }));
            fetchComments(postId);
        } catch (error) {
            setError(error.response?.data || 'Failed to create comment');
        }
    };

    const handleLikePost = async (postId) => {
        try {
            const response = await axios.post(`http://localhost:8080/api/likes/post/${postId}`,
                {},
                { withCredentials: true }
            );
            setSuccess(response.data);
            fetchLikes(postId);
        } catch (error) {
            setError(error.response?.data || 'Failed to like post');
        }
    };

    const handleUnlikePost = async (postId) => {
        try {
            const response = await axios.delete(`http://localhost:8080/api/likes/post/${postId}`,
                { withCredentials: true }
            );
            setSuccess(response.data);
            fetchLikes(postId);
        } catch (error) {
            setError(error.response?.data || 'Failed to unlike post');
        }
    };

    const handleAddTag = async (e, postId) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        try {
            const response = await axios.post(`http://localhost:8080/api/tags/post/${postId}`,
                { name: tagContent[postId] || '' },
                { withCredentials: true }
            );
            setSuccess(response.data);
            setTagContent(prev => ({ ...prev, [postId]: '' }));
            fetchTags(postId);
        } catch (error) {
            setError(error.response?.data || 'Failed to add tag');
        }
    };

    useEffect(() => {
        fetchPosts();
    }, []);

    return (
        <div className="posts-container">
            <h2>Posts</h2>
            {error && <p className="error">{error}</p>}
            {success && <p className="success">{success}</p>}
            <form onSubmit={handleCreatePost}>
                <div>
                    <textarea
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        placeholder="What's on your mind?"
                        required
                    />
                </div>
                <button type="submit">Post</button>
            </form>
            <div className="posts-list">
                {posts.map(post => (
                    <div key={post.id} className="post">
                        <p><strong>{post.user.username}</strong>: {post.content}</p>
                        <p>{new Date(post.createdAt).toLocaleString()}</p>
                        <div className="likes-section">
                            <p>Likes: {post.likes ? post.likes.length : 0}</p>
                            <button onClick={() => handleLikePost(post.id)}>Like</button>
                            <button onClick={() => handleUnlikePost(post.id)}>Unlike</button>
                            <button onClick={() => fetchLikes(post.id)}>Load Likes</button>
                        </div>
                        <div className="tags-section">
                            <p>Tags: {post.tags ? post.tags.map(tag => tag.name).join(', ') : 'None'}</p>
                            <button onClick={() => fetchTags(post.id)}>Load Tags</button>
                            <form onSubmit={(e) => handleAddTag(e, post.id)}>
                                <input
                                    type="text"
                                    value={tagContent[post.id] || ''}
                                    onChange={(e) => setTagContent(prev => ({ ...prev, [post.id]: e.target.value }))}
                                    placeholder="Add a tag..."
                                    required
                                />
                                <button type="submit">Add Tag</button>
                            </form>
                        </div>
                        <div className="comments-section">
                            <h4>Comments</h4>
                            <button onClick={() => fetchComments(post.id)}>Load Comments</button>
                            {post.comments && post.comments.map(comment => (
                                <div key={comment.id} className="comment">
                                    <p><strong>{comment.user.username}</strong>: {comment.content}</p>
                                    <p>{new Date(comment.createdAt).toLocaleString()}</p>
                                </div>
                            ))}
                            <form onSubmit={(e) => handleCreateComment(e, post.id)}>
                                <textarea
                                    value={commentContent[post.id] || ''}
                                    onChange={(e) => setCommentContent(prev => ({ ...prev, [post.id]: e.target.value }))}
                                    placeholder="Add a comment..."
                                    required
                                />
                                <button type="submit">Comment</button>
                            </form>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default Posts;