package io.github.reoseah.hematurgy;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hematurgy implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("hematurgy");

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
	}
}