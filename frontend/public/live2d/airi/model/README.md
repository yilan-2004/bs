# Live2D 模型放置说明

当前目录用于放置 AIRI 或其他 Live2D Cubism 4 模型文件。

默认配置文件：

`frontend/src/assets/live2d/airi/airi.config.json`

默认模型入口：

`/live2d/airi/model/airi.model3.json`

请将模型文件放在本目录中，例如：

- `airi.model3.json`
- `textures/`
- `motions/`
- `expressions/`
- 其他模型依赖文件

如果模型文件不存在，前端会显示虚拟助教 fallback 占位，不会导致页面白屏。
