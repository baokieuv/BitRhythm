package com.bit_rhythm.content_service.block;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;

    public void handleEvent(BlockEvent blockEvent){
        Optional<UserBlock> block = blockRepository.findByBlockerIdAndBlockedId(blockEvent.blockerId(), blockEvent.blockedId());

        if (block.isPresent()) {
            if (!blockEvent.isBlocked()) {
                blockRepository.delete(block.get());
            }
        } else {
            if (blockEvent.isBlocked()) {
                UserBlock newBlock = new UserBlock();
                newBlock.setBlockerId(blockEvent.blockerId());
                newBlock.setBlockedId(blockEvent.blockedId());
                blockRepository.save(newBlock);
            }
        }
    }
}
