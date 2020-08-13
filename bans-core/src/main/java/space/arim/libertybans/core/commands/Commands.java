/* 
 * LibertyBans-core
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * LibertyBans-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * LibertyBans-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with LibertyBans-core. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Affero General Public License.
 */
package space.arim.libertybans.core.commands;

import java.util.List;
import java.util.Locale;

import space.arim.api.configure.ConfigAccessor;

import space.arim.libertybans.core.LibertyBansCore;
import space.arim.libertybans.core.env.CmdSender;

public class Commands {

	final LibertyBansCore core;
	
	private final List<SubCommandGroup> subCommands;
	
	public static final String BASE_COMMAND_NAME = "libertybans";
	
	public Commands(LibertyBansCore core) {
		this.core = core;
		subCommands = List.of(new PunishCommands(this), new UnpunishCommands(this), new ReloadCommands(this));
	}
	
	// Shortcut access for convenience
	
	ConfigAccessor config() {
		return core.getConfigs().getConfig();
	}
	
	ConfigAccessor messages() {
		return core.getConfigs().getMessages();
	}
	
	// Main command handler
	
	public void execute(CmdSender sender, CommandPackage command) {
		if (messages().getBoolean("all.prefix.use")) {
			sender = new PrefixedCmdSender(core, sender, core.getFormatter().parseMessage(messages().getString("all.prefix.value")));
		}
		if (!sender.hasPermission("libertybans.commands")) {
			sender.parseThenSend(messages().getString("all.base-permission-message"));
			return;
		}
		if (core.getFormatter().isJsonEnabled()) {
			// Prevent JSON injection
			String args = command.clone().allRemaining();
			if (args.indexOf('|') != -1) {
				sender.parseThenSend(config().getString("json.illegal-char"));
				return;
			}
		}
		if (!command.hasNext()) {
			sender.parseThenSend(messages().getString("all.usage"));
			return;
		}
		String firstArg = command.next().toLowerCase(Locale.ENGLISH);
		for (SubCommandGroup subCommand : subCommands) {
			if (subCommand.matches.contains(firstArg)) {
				subCommand.execute(sender, command, firstArg);
				break;
			}
		}
	}
	
}