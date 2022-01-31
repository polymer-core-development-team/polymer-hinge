package com.teampolymer.polymer.hinge.common.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 保存一台多方快结构机器的区块加载状态
 * ref = 0 : 没有加载
 * total = 0 : 机器没有初始化
 * total > 0 && ref > 0 && total == ref : 机器处于加载的区块
 * total > 0 && ref > 0 && total > ref : 机器只有一半再加载的区块
 */
public class MultiblockLoadRef {
    private static final Logger logger = LogManager.getLogger();
    private int ref = 0;
    private int total = 0;

    public static MultiblockLoadRef createLoaded(int chunks) {
        if (chunks <= 0) {
            throw new IllegalArgumentException("chunks must be positive");
        }
        MultiblockLoadRef result = new MultiblockLoadRef();
        result.ref = result.total = chunks;
        return result;
    }

    public MultiblockLoadStatus getStatus() {
        if (total == 0) {
            return MultiblockLoadStatus.NOT_INITIALIZED;
        }
        if (total < 0) {
            return MultiblockLoadStatus.ERROR;
        }
        if (ref == 0) {
            return MultiblockLoadStatus.UNLOADED;
        }
        if (ref == total) {
            return MultiblockLoadStatus.LOADED;
        }
        return MultiblockLoadStatus.PARTIAL_LOAD;
    }

    public int getRef() {
        return ref;
    }

    public void setRef(int ref) {
        this.ref = ref;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        if (total < 0) {
            throw new IllegalArgumentException("total must be positive");
        }
        if (this.total >= 0) {
            this.total = total;
        }
    }

    public int incRef() {
        if (total == 0) {
            logger.error("Expected Loaded chunks is less or equal than zero, multiblock may not be correctly loaded");
            total = -1;
        }
        ref++;
        if (ref > total) {
            logger.error("Multiblock loaded in more chunks than expected");
            total = -1;
        }
        return ref;
    }

    public int decRef() {
        ref--;
        if (ref < 0) {
            logger.error("Ref must be positive");
            total = -1;
        }
        return ref;
    }
}
