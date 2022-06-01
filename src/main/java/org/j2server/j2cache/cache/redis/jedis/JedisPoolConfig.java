package org.j2server.j2cache.cache.redis.jedis;

import java.time.Duration;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * redis配置项 </br>
 *
 * @author xiexb
 */
public class JedisPoolConfig {
    private Boolean testOnCreate;
    private Boolean testOnBorrow;
    private Boolean testOnReturn;
    private Boolean testWhileIdle;
    private Long minEvictableIdleTimeMillis;
    private Long timeBetweenEvictionRunsMillis;
    private Integer numTestsPerEvictionRun;
    private Integer maxAttempts;
    private Integer maxTotal;
    private Integer maxIdle;
    private Integer minIdle;
    private Integer maxWaitMillis;

    public Boolean getTestOnCreate() {
        return testOnCreate;
    }

    public void setTestOnCreate(Boolean testOnCreate) {
        this.testOnCreate = testOnCreate;
    }

    public Boolean getTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(Boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public Boolean getTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(Boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public Boolean getTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(Boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public Long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(Long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public Long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(Long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public Integer getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    public void setNumTestsPerEvictionRun(Integer numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Integer getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(Integer maxTotal) {
        this.maxTotal = maxTotal;
    }

    public Integer getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(Integer maxIdle) {
        this.maxIdle = maxIdle;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public Integer getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public void setMaxWaitMillis(Integer maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

	public static <T> GenericObjectPoolConfig<T> buildPoolConfig(JedisPoolConfig config) {
        GenericObjectPoolConfig<T> poolConfig = new GenericObjectPoolConfig<>();
		if (config == null) {
			return poolConfig;
		}
		
        if (config.getTestWhileIdle() != null) {
            poolConfig.setTestWhileIdle(config.getTestWhileIdle());
        }

        if (config.getTestOnBorrow() != null) {
            poolConfig.setTestOnBorrow(config.getTestOnBorrow());
        }

        if (config.getTestOnCreate() != null) {
            poolConfig.setTestOnCreate(config.getTestOnCreate());
        }

        if (config.getTestOnReturn() != null) {
            poolConfig.setTestOnReturn(config.getTestOnReturn());
        }

        if (config.getMinEvictableIdleTimeMillis() != null) {
            poolConfig.setMinEvictableIdleTime(Duration.ofMillis(config.getMinEvictableIdleTimeMillis()));
        }

        if (config.getTimeBetweenEvictionRunsMillis() != null) {
            poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(config.getTimeBetweenEvictionRunsMillis()));
        }

        if (config.getNumTestsPerEvictionRun() != null) {
            poolConfig.setNumTestsPerEvictionRun(config.getNumTestsPerEvictionRun());
        }

        if (config.getMaxTotal() != null) {
            poolConfig.setMaxTotal(config.getMaxTotal());
        }

        if (config.getMaxIdle() != null) {
            poolConfig.setMaxIdle(config.getMaxIdle());
        }

        if (config.getMinIdle() != null) {
            poolConfig.setMinIdle(config.getMinIdle());
        }

        if (config.getMaxWaitMillis() != null) {
            poolConfig.setMaxWait(Duration.ofMillis(config.getMaxWaitMillis()));
        }

        return poolConfig;
    }
}
