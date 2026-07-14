package com.minitrello.exception;

public class BoardMemberNotFoundException extends RuntimeException {
  public BoardMemberNotFoundException(String message) {
    super(message);
  }
}
