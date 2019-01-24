void call(Closure body){
    errors = []

    body()

    if(!errors.empty) {
        error errors.join("; ")
    }
}