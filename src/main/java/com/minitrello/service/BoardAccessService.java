package com.minitrello.service;

import com.minitrello.entity.Board;
import com.minitrello.entity.User;
import com.minitrello.exception.BoardNotFoundException;
import com.minitrello.exception.ForbiddenOperationException;
import com.minitrello.exception.UserNotFoundException;
import com.minitrello.repository.BoardMemberRepository;
import com.minitrello.repository.BoardRepository;
import com.minitrello.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardAccessService {
    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final UserRepository userRepository;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    public Board getBoardOrThrow(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found with id " + boardId));
    }

    public boolean isOwner(Board board, User user) {
        return board.getOwner().getId().equals(user.getId());
    }

    public boolean isMember(Board board, User user) {
        return boardMemberRepository.existsByBoardIdAndUserId(board.getId(), user.getId());
    }

    public void assertHasAccess(Board board, User user) {
        if (!isOwner(board, user) && !isMember(board, user)) {
            throw new ForbiddenOperationException("You do not have access to this board");
        }
    }

    public void assertIsOwner(Board board, User user) {
        if (!isOwner(board, user)) {
            throw new ForbiddenOperationException("Only the board owner can perform this action");
        }
    }
}
