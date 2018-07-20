package com.vanda.platform.saturn.core.context.repository;

/**
 * TODO 未写注释
 * @author yinwenjie
 */
public interface RepositoryBuilder {
  public <T extends RepositoryBuilder> T build();
}