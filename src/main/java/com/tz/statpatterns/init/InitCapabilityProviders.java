
/*
 * Probability Pattern for AE2
 * Copyright (C) 2026 TaoLe-si
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.tz.statpatterns.init;

import appeng.api.AECapabilities;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.core.definitions.AEBlockEntities;
import com.tz.statpatterns.core.definition.SPBlockEntities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class InitCapabilityProviders {
    private InitCapabilityProviders() {
    }
    public static void register(RegisterCapabilitiesEvent event) {
        initPatternProvider(event);
    }
    private static void initPatternProvider(RegisterCapabilitiesEvent event) {

    }
}
