import axios from 'axios';
import { useState, useEffect } from 'react';
import './Posts.css';

function Posts() {
    const [content, setContent] = useState('');
    const [imageFile, setImageFile] = useState(null);
    const [posts, setPosts] = useState([]);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [commentContent, setCommentContent] = useState({});
    const [tagContent, setTagContent] = useState({});
    const [showTagForm, setShowTagForm] = useState({});
    const [showCommentForm, setShowCommentForm] = useState({});
    const [showCreatePostForm, setShowCreatePostForm] = useState(false);
    const [showFilterTags, setShowFilterTags] = useState(false);
    const [allTags, setAllTags] = useState([]);
    const [selectedTags, setSelectedTags] = useState([]);
    const [editPostId, setEditPostId] = useState(null);
    const [editContent, setEditContent] = useState('');
    const [currentUserEmail, setCurrentUserEmail] = useState('');

    const fetchPosts = async () => {
        try {
            const url = selectedTags.length > 0
                ? `http://localhost:8080/api/posts/filter?tags=${selectedTags.join('&tags=')}`
                : 'http://localhost:8080/api/posts';
            const response = await axios.get(url, { withCredentials: true });
            const postsData = response.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            setPosts(postsData);

            for (const post of postsData) {
                await Promise.all([
                    fetchLikes(post.id),
                    fetchTags(post.id),
                    fetchComments(post.id)
                ]);
            }
        } catch (error) {
            setError(typeof error.response?.data === 'string' ? error.response.data : error.response?.data?.message || 'Failed to load posts');
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
            setError('Comments functionality is not implemented yet');
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

    const fetchAllTags = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/tags', {
                withCredentials: true
            });
            setAllTags(response.data);
        } catch (error) {
            setError('Failed to load tags');
        }
    };

    const handleCreatePost = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        try {
            const formData = new FormData();
            formData.append('content', content);
            if (imageFile) {
                formData.append('image', imageFile);
            }

            const response = await axios.post('http://localhost:8080/api/posts',
                formData,
                { withCredentials: true }
            );
            setSuccess('Post created successfully');
            setContent('');
            setImageFile(null);
            document.getElementById('imageInput').value = '';
            setShowCreatePostForm(false);
            fetchPosts();
        } catch (error) {
            setError(typeof error.response?.data === 'string' ? error.response.data : error.response?.data?.message || 'Failed to create post');
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
            setError('Comments functionality is not implemented yet');
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
            setError(typeof error.response?.data === 'string' ? error.response.data : error.response?.data?.message || 'Failed to like post');
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
            setError(typeof error.response?.data === 'string' ? error.response.data : error.response?.data?.message || 'Failed to unlike post');
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
            fetchAllTags();
        } catch (error) {
            setError(typeof error.response?.data === 'string' ? error.response.data : error.response?.data?.message || 'Failed to add tag');
        }
    };

    const handleDeletePost = async (postId) => {
        try {
            const response = await axios.delete(`http://localhost:8080/api/posts/${postId}`, {
                withCredentials: true
            });
            setSuccess(typeof response.data === 'string' ? response.data : response.data?.message || 'Post deleted successfully');
            setPosts(prevPosts => prevPosts.filter(post => post.id !== postId));
        } catch (error) {
            setError(typeof error.response?.data === 'string' ? error.response.data : error.response?.data?.message || 'Failed to delete post');
        }
    };

    const handleEditPost = async (postId) => {
        try {
            const formData = new FormData();
            formData.append('content', editContent);
            const response = await axios.put(`http://localhost:8080/api/posts/${postId}`,
                formData,
                { withCredentials: true }
            );
            setSuccess('Post updated successfully');
            setEditPostId(null);
            setEditContent('');
            fetchPosts();
        } catch (error) {
            setError(typeof error.response?.data === 'string' ? error.response.data : error.response?.data?.message || 'Failed to edit post');
        }
    };

    const toggleTagForm = (postId) => {
        setShowTagForm(prev => ({ ...prev, [postId]: !prev[postId] }));
    };

    const toggleCommentForm = (postId) => {
        setShowCommentForm(prev => ({ ...prev, [postId]: !prev[postId] }));
    };

    const handleTagSelection = (tag) => {
        setSelectedTags(prev =>
            prev.includes(tag)
                ? prev.filter(t => t !== tag)
                : [...prev, tag]
        );
    };

    const isCurrentUserPost = (post) => {
        return post.user.email === currentUserEmail;
    };

    useEffect(() => {
        const fetchCurrentUserEmail = async () => {
            try {
                const response = await axios.get('http://localhost:8080/api/users/me', { withCredentials: true });
                setCurrentUserEmail(response.data.email);
            } catch (error) {
                setError('Failed to load current user');
            }
        };
        fetchCurrentUserEmail();
        fetchPosts();
        fetchAllTags();
    }, [selectedTags]);

    return (
        <div className="posts-container">
            <h2>Posts</h2>
            {error && <p className="error">{error}</p>}
            {success && <p className="success">{success}</p>}
            <div className="top-buttons">
                <button className="create-post-button" onClick={() => setShowCreatePostForm(!showCreatePostForm)}>
                    {showCreatePostForm ? 'Hide Form' : 'Create Post'}
                </button>
                <button className="filter-button" onClick={() => setShowFilterTags(!showFilterTags)}>
                    {showFilterTags ? 'Hide Filters' : 'Filter by Tag'}
                </button>
            </div>
            {showFilterTags && (
                <div className="filter-tags">
                    <h3>Select Tags</h3>
                    {allTags.length > 0 ? (
                        allTags.map(tag => (
                            <label key={tag} className="tag-checkbox">
                                <input
                                    type="checkbox"
                                    checked={selectedTags.includes(tag)}
                                    onChange={() => handleTagSelection(tag)}
                                />
                                {tag}
                            </label>
                        ))
                    ) : (
                        <p>No tags available</p>
                    )}
                </div>
            )}
            {showCreatePostForm && (
                <form onSubmit={handleCreatePost} encType="multipart/form-data">
                    <div>
                        <textarea
                            value={content}
                            onChange={(e) => setContent(e.target.value)}
                            placeholder="What's on your mind?"
                            required
                        />
                        <input
                            id="imageInput"
                            type="file"
                            accept="image/*"
                            onChange={(e) => setImageFile(e.target.files[0])}
                            className="image-input"
                        />
                    </div>
                    <button type="submit" className="small-button">Post</button>
                </form>
            )}
            <div className="posts-list">
                {posts.map(post => (
                    <div key={post.id} className="post">
                        <div className="post-content">
                            <p><strong>{post.user.username}</strong>: {post.content}</p>
                        </div>
                        {post.imageUrl && (
                            <div className="post-image-container">
                                <img
                                    src={post.imageUrl}
                                    alt={`${post.user.username}'s post`}
                                    className="post-image"
                                    onError={(e) => console.error('Image failed to load:', post.imageUrl)}
                                />
                            </div>
                        )}
                        <div className="post-meta">
                            <span>{new Date(post.createdAt).toLocaleString()}</span>
                            <span>Likes: {post.likes ? post.likes.length : 0}</span>
                        </div>
                        <div className="action-buttons-container">
                            <button className="small-button" onClick={() => handleLikePost(post.id)}>Like</button>
                            <button className="small-button" onClick={() => handleUnlikePost(post.id)}>Dislike</button>
                            <button className="small-button" onClick={() => toggleTagForm(post.id)}>Add Tag</button>
                            <button className="small-button" onClick={() => toggleCommentForm(post.id)}>Add Comment</button>
                            {isCurrentUserPost(post) && (
                                <>
                                    <button className="small-button" onClick={() => {
                                        setEditPostId(post.id);
                                        setEditContent(post.content);
                                    }}>Edit</button>
                                    <button className="small-button" onClick={() => handleDeletePost(post.id)}>Delete</button>
                                </>
                            )}
                        </div>
                        {editPostId === post.id && (
                            <form onSubmit={(e) => handleEditPost(post.id)} className="edit-post-form">
                                <textarea
                                    value={editContent}
                                    onChange={(e) => setEditContent(e.target.value)}
                                    required
                                />
                                <button type="submit" className="small-button">Save</button>
                                <button type="button" className="small-button" onClick={() => setEditPostId(null)}>Cancel</button>
                            </form>
                        )}
                        {showTagForm[post.id] && (
                            <div className="tags-section">
                                {post.tags && post.tags.length > 0 && (
                                    <div className="existing-tags">
                                        <p>Existing Tags: {post.tags.map(tag => tag.name).join(', ')}</p>
                                    </div>
                                )}
                                <form onSubmit={(e) => handleAddTag(e, post.id)}>
                                    <input
                                        type="text"
                                        value={tagContent[post.id] || ''}
                                        onChange={(e) => setTagContent(prev => ({ ...prev, [post.id]: e.target.value }))}
                                        placeholder="Add a tag..."
                                        required
                                    />
                                    <button type="submit" className="small-button">Submit Tag</button>
                                </form>
                            </div>
                        )}
                        {showCommentForm[post.id] && (
                            <div className="comments-section">
                                {post.comments && post.comments.length > 0 ? (
                                    post.comments.map(comment => (
                                        <div key={comment.id} className="comment">
                                            <p><strong>{comment.user.username}</strong>: {comment.content}</p>
                                            <p>{new Date(comment.createdAt).toLocaleString()}</p>
                                        </div>
                                    ))
                                ) : null}
                                <form onSubmit={(e) => handleCreateComment(e, post.id)}>
                                    <textarea
                                        value={commentContent[post.id] || ''}
                                        onChange={(e) => setCommentContent(prev => ({ ...prev, [post.id]: e.target.value }))}
                                        placeholder="Add a comment..."
                                        required
                                    />
                                    <button type="submit" className="small-button">Submit Comment</button>
                                </form>
                            </div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
}

export default Posts;