package net.minecraftforge.fml.client.event;

import net.minecraft.client.renderer.entity.layers.VillagerLevelPendantLayer;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.Event;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;

import java.util.*;

/**
 * Fired just before {@link IReloadableResourceManager#initialReload} so mods can register listeners.
 * Fired on the mod event bus.
 *
 * Called on {@link Dist#CLIENT} - the game client.
 */
public class RegisterReloadListenersEvent extends Event
{
	private final IReloadableResourceManager resourceManager;

	private Map<ResourceLocation, IFutureReloadListener>  listeners = new HashMap<>();
	private Map<ResourceLocation, List<ResourceLocation>> before = new HashMap<>(); //foreach listener all the listeners to be run before it

	public RegisterReloadListenersEvent(IReloadableResourceManager resourceManager)
	{
		this.resourceManager = resourceManager;

		ResourceLocation last = null;
		for(Map.Entry<ResourceLocation, IFutureReloadListener> entry : resourceManager.getAndResetReloaders().entrySet()){
			System.out.println(entry.getKey());
			listeners.put(entry.getKey(), entry.getValue());
			if(last != null)
				before.computeIfAbsent(entry.getKey(), $ -> new ArrayList<>()).add(last);
			last = entry.getKey();
		}
		addReloadListener(new ResourceLocation("test", "dummy"), ((IResourceManagerReloadListener)((k)->{})),
				new ResourceLocation[]{new ResourceLocation("model_manager")},
				new ResourceLocation[]{new ResourceLocation("splashes")});
	}

	public void addReloadListener(ResourceLocation name, IFutureReloadListener listener, ResourceLocation[] runBefore, ResourceLocation[] runAfter)
	{
		listeners.put(name, listener);
		Arrays.stream(runBefore).forEach(rl -> before.computeIfAbsent(rl, $ -> new ArrayList<>()).add(name));
		Arrays.stream(runAfter).forEach(before.computeIfAbsent(name, $ -> new ArrayList<>())::add);
	}

	public void finish(){
		List<ResourceLocation> ordered = new LinkedList<>();
		Set<ResourceLocation> marked = new HashSet<>(), done = new HashSet<>();
		this.listeners.keySet().forEach(k -> ordered.addAll(toposort(k, marked, done)));
		System.out.println("Ordering: "+ordered);
		ordered.forEach(k -> resourceManager.addReloadListener(k, listeners.get(k)));
	}

	private List<ResourceLocation> toposort(ResourceLocation name, Set<ResourceLocation> marked, Set<ResourceLocation> done){
		if(!marked.add(name))
			throw new IllegalArgumentException("Zyclic dependency for reload listener "+name);
		List<ResourceLocation> result = new LinkedList<>();
		if(done.add(name) && this.listeners.containsKey(name)) {
			if(this.before.containsKey(name))
				this.before.get(name).forEach(rl -> result.addAll(toposort(rl, marked, done)));
			result.add(name);
		}
		marked.remove(name);
		return result;
	}

}
