/**
 * Copyright (C) 2014 Apigee Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apigee.buildTools.enterprise4g.rest;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.EnumSet;

import static io.apigee.buildTools.enterprise4g.rest.ActionFlags.*;

public class ActionFlagsTest {

	@Test
	public void testActionSetFill() {

		EnumSet<ActionFlags> set = EnumSet.noneOf(ActionFlags.class);
		assertFalse(set.contains(VALIDATE));
		assertFalse(set.contains(UPDATE));
		assertFalse(set.contains(FORCE));

		set.add(VALIDATE);
		assertTrue(set.contains(VALIDATE));
		assertFalse(set.contains(UPDATE));
		assertFalse(set.contains(FORCE));

	}

	@Test
	public void testActionSetCombined() {
		EnumSet<ActionFlags> set = EnumSet.noneOf(ActionFlags.class);
		set.add(VALIDATE);
		set.add(FORCE);
		assertTrue(set.contains(VALIDATE));
		assertFalse(set.contains(UPDATE));
		assertTrue(set.contains(FORCE));
	}

	@Test
	public void testValueOf() {
		assertEquals(VALIDATE, ActionFlags.valueOfIgnoreCase("validate"));
		assertEquals(CLEAN, ActionFlags.valueOfIgnoreCase("clean"));
		assertEquals(OVERRIDE, ActionFlags.valueOfIgnoreCase("overridE"));
	}

	@Test
	public void testValueOfNotFound() {
		assertEquals(VALIDATE, ActionFlags.valueOfIgnoreCase("invalid"));
	}

}
