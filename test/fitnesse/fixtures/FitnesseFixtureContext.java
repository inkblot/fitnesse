// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.authentication.Authenticator;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.wiki.WikiPage;
import util.Clock;

public class FitnesseFixtureContext {
    public static WikiPage root;
    public static WikiPage page;
    public static Response response;
    public static MockResponseSender sender;
    public static String baseDir = "build/temp";
    public static FitNesseContext context;
    public static FitNesse fitnesse;
    public static Clock clock;
    public static Authenticator authenticator;
}
