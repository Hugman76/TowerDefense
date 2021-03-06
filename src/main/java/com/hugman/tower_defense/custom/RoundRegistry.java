package com.hugman.tower_defense.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hugman.tower_defense.TowerDefense;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;

public class RoundRegistry {
	private static final TinyRegistry<Round> ROUNDS = TinyRegistry.newStable();

	public static void init() {
		ResourceManagerHelper serverData = ResourceManagerHelper.get(ResourceType.SERVER_DATA);

		serverData.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return TowerDefense.id("rounds");
			}

			@Override
			public void apply(ResourceManager manager) {
				ROUNDS.clear();
				Collection<Identifier> resources = manager.findResources("rounds", path -> path.endsWith(".json"));

				for(Identifier path : resources) {
					try {
						Resource resource = manager.getResource(path);
						try(Reader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
							JsonElement json = new JsonParser().parse(reader);
							Identifier identifier = identifierFromPath(path);
							DataResult<Round> result = Round.CODEC.decode(JsonOps.INSTANCE, json).map(Pair::getFirst);
							result.result().ifPresent(game -> {
								ROUNDS.register(identifier, game);
								TowerDefense.LOGGER.info("Registered rounds " + identifier);
							});
							result.error().ifPresent(error -> TowerDefense.LOGGER.error("Failed to decode rounds at {}: {}", path, error.toString()));
						}
					}
					catch(IOException e) {
						TowerDefense.LOGGER.error("Failed to decode rounds at {}", path, e);
					}
				}
			}
		});
	}

	private static Identifier identifierFromPath(Identifier location) {
		String path = location.getPath();
		path = path.substring("rounds/".length(), path.length() - ".json".length());
		return new Identifier(location.getNamespace(), path);
	}

	@Nullable
	public static Round get(int index) {
		return ROUNDS.get(TowerDefense.id(String.valueOf(index)));
	}

	public static int getId(Round round) {
		return Integer.parseInt(ROUNDS.getIdentifier(round).getPath());
	}

	public static TinyRegistry<Round> getRounds() {
		return ROUNDS;
	}
}
