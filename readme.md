mDynamicLoader é um plugin para forçar o carregamento de blocos, entidades e semelhantes de plugins em um contexto onde é necessário carregar/descarregar mundos. Isso é relevante em plugins como o ExecutableBlocks, MythicMobs e outros que não fornecem suporte nativo a esses contextos.
Nota: Para funcionamento adequado, é necessário que nesse contexto de carregar/descarregar, seja chamado o evento 'WorldLoadEvent'.

## ExecutableBlocks
- O plugin armazena a posição do bloco e a posição do jogador na hora que o jogador place um bloco do EB. Na hora do carregamento do mundo, ele cria um 'NPC' (que é um Player entity) invisível e posiciona o bloco conforme a posição do NPC.
- O processo envolve remover o bloco e inserir outro na mesma posição, o que faz com que seja impossível permitir a persistência de dados do bloco, como em um baú por exemplo. Ainda sim, é interessante armazenar blocos customizados com funcionalidades ainda que não seja possível salvar inventários, por exemplo.
- O plugin usa SQL para armazenar as posições, pois é esperado de funcionar em um contexto onde possui um mesmo mundo que pode ser carregado/descarregado em servidores diferentes, então não depende do armazenamento em arquivo padrão do ExecutableBlocks.

```sql
CREATE TABLE IF NOT EXISTS eb_blocks (
    loader_block_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    world_name VARCHAR(100) NOT NULL,
    position_serialized VARCHAR(100) NOT NULL UNIQUE,
    block_id VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS loader_entities (
    loader_block_id INT NOT NULL,
    target_position_serialized VARCHAR(200) NOT NULL,
    FOREIGN KEY (loader_block_id) REFERENCES eb_blocks(loader_block_id)
);
```
Estrutura das tabelas acima.

Na classe LoadEvents[link pra classe], note a seguinte parte:
```kotlin
private fun placeExecutableBlock(loaderBlockId: Int, executableBlockId: String, targetLocation: Location, blockLocation: Location) {
        val executableBlock: ExecutableBlock = ExecutableBlocksAPI.getExecutableBlocksManager().getExecutableBlock(executableBlockId).orElse(null) ?: return

        if (blockLocation.block.type != Material.AIR) {
            blockLocation.block.type = Material.AIR
        }

        val npc = InvisibleNPC(loaderBlockId, targetLocation, blockLocation)
        val npcPlayer = npc.generate()

        executableBlock.place(
            blockLocation,
            true,
            OverrideEBP.REMOVE_EXISTING_EBP,
            npcPlayer,
            null,
            null
        )

        npc?.remove()
    }
```

Existe um problema que compromete o funcionamento do plugin: o bloco não é gerado baseado na posição do jogador. Então isso gera uma posição diferente da qual deveria estar.
