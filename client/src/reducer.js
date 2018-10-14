const initialState = {
    jobs: []
};

const reducer = (state = initialState, action) => {
    console.log('reducer', state, action);
    switch (action.type) {
        case 'GOT_JOBS':
            return {...state, jobs: action.jobs};
        default:
            return state;
    }
};

export default reducer;
