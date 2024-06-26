#!/bin/bash

set -e

# Run a build script inside a container

DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

CONTAINER_NAME=$1
CONTEXT_DIR=$2
DOCKERFILE=$3

if [ -z "${CONTAINER_NAME}" ]; then
    echo "specify container name as the first argument. and the script and args as the second arguments"
fi;

if [ -z "${CONTEXT_DIR}" ]; then
    echo "specify a context dir as the second argument"
fi;

if [ -z "${DOCKERFILE}" ]; then
    echo "no docker file specified. Defaulting to Dockerfile"
    DOCKERFILE=Dockerfile
fi

GIT_COMMIT=$(git rev-parse HEAD)

ECR_CACHE_REPOSITORY=297820887526.dkr.ecr.eu-west-1.amazonaws.com/build-cache


# Use these as caches, hopefully some of the last n commits have been built in CI.
CACHE_COMMIT1=${ECR_CACHE_REPOSITORY}:${CONTAINER_NAME}-$(git rev-parse @~1)
CACHE_COMMIT2=${ECR_CACHE_REPOSITORY}:${CONTAINER_NAME}-$(git rev-parse @~2)
CACHE_COMMIT3=${ECR_CACHE_REPOSITORY}:${CONTAINER_NAME}-$(git rev-parse @~3)
CACHE_COMMIT4=${ECR_CACHE_REPOSITORY}:${CONTAINER_NAME}-$(git rev-parse @~4)
CACHE_COMMIT5=${ECR_CACHE_REPOSITORY}:${CONTAINER_NAME}-$(git rev-parse @~5)

# This is the resulting image name
IMAGE_COMMIT=${ECR_CACHE_REPOSITORY}:${CONTAINER_NAME}-$(git rev-parse HEAD)

aws ecr get-login-password --region eu-west-1 | docker login --username AWS --password-stdin 297820887526.dkr.ecr.eu-west-1.amazonaws.com

if [ "$SKIP_CONTAINER_PUSH" != "" ]; then
    PUSH=""
else
    PUSH="--push"
fi

docker build --pull ${PUSH} -t ${IMAGE_COMMIT} --build-arg BUILDKIT_INLINE_CACHE=1 \
--cache-from type=registry,ref=${IMAGE_COMMIT} \
--cache-from type=registry,ref=${CACHE_COMMIT1} \
--cache-from type=registry,ref=${CACHE_COMMIT2} \
--cache-from type=registry,ref=${CACHE_COMMIT3} \
--cache-from type=registry,ref=${CACHE_COMMIT4} \
--cache-from type=registry,ref=${CACHE_COMMIT5} \
${CONTEXT_DIR}

docker tag ${IMAGE_COMMIT} ${CONTAINER_NAME}
