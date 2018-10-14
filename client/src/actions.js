export const getJobs = () => {
    return async (dispatch) => {
        const resp = await fetch('/api/jobs');
        const jobs = await resp.json();
        const action = gotJobs(jobs);
        dispatch(action);
    }
};

export const gotJobs = (jobs) => {
    return {
        type: 'GOT_JOBS',
        jobs
    }
};
