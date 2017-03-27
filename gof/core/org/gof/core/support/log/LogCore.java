package org.gof.core.support.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogCore {
	public static Logger core = LoggerFactory.getLogger("CORE");
	public static Logger conn = LoggerFactory.getLogger("CORE_CONN");
	public static Logger db = LoggerFactory.getLogger("CORE_DB");
	public static Logger msg = LoggerFactory.getLogger("CORE_MSG");
	public static Logger remote = LoggerFactory.getLogger("CORE_REMOTE");
	public static Logger effect = LoggerFactory.getLogger("CORE_EFFECT");
	public static Logger temp = LoggerFactory.getLogger("TEMP");
}