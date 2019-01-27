### 歌词绘制核心思想 （中间行其实就是选中当前播放的行）

   - 确定中间行的坐标绘制，其它行则由 行号加行高控制

### 播放滚动 （全部针对中间行的偏移，行高是固定的）

   - 行高距离 内 中间行向上移到一行的时间比

   - 确认行的偏移时长，中间行上移一行可用时间 是由 下一行（即将成为中间行）的起始时间-当前中间行的偏移时长

   - 偏移百分比 是由当前播放进度-中间行的起始时间

   - 偏移量  是由偏移百分比 * 行高

### 手动滚动

   - 当前滚动距离超过行高时，重新确定中间行

   - 中间行变换后，滚动坐标重新计算






### License

    Copyright (c) 2019 anyrsan

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
