package com.denote.client.concurrent

import com.denote.client.dto.APIResponse

interface MissileRunnable  {
    public boolean stopMissile();
    public APIResponse startAndReturnAsyncTask();
}
