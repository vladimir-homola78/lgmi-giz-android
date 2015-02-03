package com.ibrow.de.giz.siegelklarheit;

/**
 * Test API using identifeye.ibrow.com on port 5001.
 * @author Pete
 */
class TestIdentifeyeAPI extends IdentifeyeAPI{


    protected final String testEndPoint="http://api.siegelklarheit.de/";

    protected final String testImageBase="http://api.siegelklarheit.de/";

    @Override
    protected String getEndPoint(){
        return testEndPoint;
    }

    @Override
    protected String getImageBase(){
        return testImageBase;
    }

    public String getWebviewBaseURL(){
        return testEndPoint;
    }

}
