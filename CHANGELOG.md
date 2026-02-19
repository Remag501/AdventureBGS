# 1.0.0 (2026-01-15)


### Bug Fixes

* **blizzard:** Fixed a syntax error. The correct enum from PotionEffect is now mentioned. ([363d853](https://github.com/Remag501/AdventureBGS/commit/363d853494805d6e0e76214346de9dd07bea7483))
* **weather:** Uses paper async place to not crash server when changing beacon colors. ([7e84c1c](https://github.com/Remag501/AdventureBGS/commit/7e84c1c96d26fd3253ebab4da6a7c49ab5b392e3))


### Features

* added new placeholders %adventurebgs_next_world_id% and %adventurebgs_current_world_id%. ([e07571f](https://github.com/Remag501/AdventureBGS/commit/e07571f9c2842d51eb0529e4d528c6b3856e14dc))
* added placeholder api hook support for commands. ([3018802](https://github.com/Remag501/AdventureBGS/commit/3018802ebace80a0b24e771e0a4de675d7eb390e))
* broadcast title with warning, and puts timer boss bar near map closure. Applies wither to correct map. Kills any offline player that disconnected in map during closure. ([a35df26](https://github.com/Remag501/AdventureBGS/commit/a35df2600e2e06b27e90e8810edf16665e1fd4a7))
* changed rejoin in map penalty to a wither effect instead of instant death ([80fda9d](https://github.com/Remag501/AdventureBGS/commit/80fda9dd33a8ede61635659e861c8ebe2bb5699c))
* **weather:** Add blizzard effect class to give players slowness and spawn snowflake particle. Called on function tick. ([d32f2ef](https://github.com/Remag501/AdventureBGS/commit/d32f2efa2392eedd2572f728461578e74cbadc94))
* **weather:** Blizzard has been updated to have its intended features. Player set freeze over time and heat source to remove it. ([fc83702](https://github.com/Remag501/AdventureBGS/commit/fc83702ae352bce9d94056a45a993fc67130d73f))
* **weather:** Introduce the 'weather_conditions' list structure in config.yml to support unique properties for Blizzard and future weather. Created empty manager and model file. ([5251a91](https://github.com/Remag501/AdventureBGS/commit/5251a914c769c9008d291e2687e144496823bb5a))
* **weather:** Manager loads weather model from config and assigns schedules weather based on frequency and intervals. ([804fd7d](https://github.com/Remag501/AdventureBGS/commit/804fd7dfea03aca098a6dc0856b39ef604a052aa))
* **weather:** Model stores data for weather conditions. ([fafc198](https://github.com/Remag501/AdventureBGS/commit/fafc1981b4164eebbad094b72567ecbb1697d484))
