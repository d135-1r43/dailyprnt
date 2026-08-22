package com.dailyprnt.modules;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Resolves the configured module ids to beans, preserving configuration order.
 */
@ApplicationScoped
public class ModuleRegistry
{
	@Inject
	Instance<Module> modules;

	@ConfigProperty(name = "dailyprnt.modules")
	List<String> enabledIds;

	public List<Module> enabled()
	{
		List<Module> resolved = new ArrayList<>();
		for (String id : enabledIds)
		{
			resolved.add(byId(id).orElseThrow(() -> new IllegalStateException(
					"Unknown module '" + id + "' in dailyprnt.modules. Available: " + availableIds())));
		}
		return resolved;
	}

	private Optional<Module> byId(String id)
	{
		return modules.stream().filter(module -> module.id().equals(id)).findFirst();
	}

	private List<String> availableIds()
	{
		return modules.stream().map(Module::id).sorted().toList();
	}
}
