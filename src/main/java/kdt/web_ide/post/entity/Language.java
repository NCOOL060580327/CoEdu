package kdt.web_ide.post.entity;

import lombok.Getter;

@Getter
public enum Language {
  JAVA("java"),
  CPP("cpp"),
  PYTHON("python"),
  JAVASCRIPT("javascript");

  private final String language;

  Language(String language) {
    this.language = language;
  }
}
